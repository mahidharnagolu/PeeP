import { View, Text, TextInput, StyleSheet, TouchableOpacity, FlatList, Alert, ActivityIndicator } from 'react-native';
import { useState, useEffect } from 'react';
import { useRouter } from 'expo-router';
import { useAuthStore } from '@/stores/authStore';
import { useFriendStore } from '@/stores/friendStore';
import { ChevronLeft, UserPlus, Check, X } from 'lucide-react-native';

export default function FriendsScreen() {
    const router = useRouter();
    const { user } = useAuthStore();
    const { pendingRequests, fetchPendingRequests, sendFriendRequest, acceptFriendRequest, rejectFriendRequest, fetchFriends } = useFriendStore();

    const [username, setUsername] = useState('');
    const [isSearching, setIsSearching] = useState(false);
    const [searchError, setSearchError] = useState('');

    useEffect(() => {
        if (user) {
            fetchPendingRequests(user.id);
        }
    }, [user]);

    const handleSearch = async () => {
        if (!user || !username.trim()) return;

        setIsSearching(true);
        setSearchError('');

        const result = await sendFriendRequest(user.id, username.trim().toLowerCase());

        if (result.error) {
            setSearchError(result.error);
        } else {
            Alert.alert('Success!', `Friend request sent to @${username.trim()}`);
            setUsername('');
        }

        setIsSearching(false);
    };

    const handleAccept = async (requestId: string) => {
        const success = await acceptFriendRequest(requestId);
        if (success && user) {
            fetchPendingRequests(user.id);
            fetchFriends(user.id);
            Alert.alert('Friend Added! 🎉');
        }
    };

    const handleReject = async (requestId: string) => {
        const success = await rejectFriendRequest(requestId);
        if (success && user) {
            fetchPendingRequests(user.id);
        }
    };

    return (
        <View style={styles.container}>
            {/* Header */}
            <View style={styles.header}>
                <TouchableOpacity onPress={() => router.back()} style={styles.backBtn}>
                    <ChevronLeft color="#FFFFFF" size={28} />
                </TouchableOpacity>
                <Text style={styles.logo}>PeeP.</Text>
                <View style={{ width: 44 }} />
            </View>

            {/* Search/Add Friend */}
            <View style={styles.searchSection}>
                <View style={styles.searchRow}>
                    <View style={styles.searchInputContainer}>
                        <TextInput
                            style={styles.input}
                            placeholder="Add or search friends"
                            placeholderTextColor="#666666"
                            value={username}
                            onChangeText={setUsername}
                            autoCapitalize="none"
                            autoCorrect={false}
                        />
                    </View>
                    <TouchableOpacity
                        style={[styles.searchBtn, (isSearching || !username.trim()) && styles.searchBtnDisabled]}
                        onPress={handleSearch}
                        disabled={isSearching || !username.trim()}
                        activeOpacity={0.7}
                    >
                        {isSearching ? (
                            <ActivityIndicator color="#000000" size="small" />
                        ) : (
                            <UserPlus color="#000000" size={18} />
                        )}
                    </TouchableOpacity>
                </View>
                {searchError ? (
                    <Text style={styles.error}>{searchError}</Text>
                ) : null}
            </View>

            {/* Pending Requests */}
            <View style={styles.requestsSection}>
                <Text style={styles.sectionTitle}>
                    FRIEND REQUESTS {pendingRequests.length > 0 ? `· ${pendingRequests.length} NEW` : ''}
                </Text>

                {pendingRequests.length === 0 ? (
                    <Text style={styles.emptyText}>No pending requests</Text>
                ) : (
                    <FlatList
                        data={pendingRequests}
                        keyExtractor={item => item.id}
                        renderItem={({ item }) => (
                            <View style={styles.requestCard}>
                                <View style={styles.requestAvatar}>
                                    <Text style={styles.requestAvatarText}>
                                        {item.user.username.charAt(0).toUpperCase()}
                                    </Text>
                                </View>
                                <View style={styles.requestInfo}>
                                    <Text style={styles.requestName}>{item.user.username}</Text>
                                    <Text style={styles.requestHandle}>@{item.user.username}</Text>
                                </View>
                                <View style={styles.requestActions}>
                                    <TouchableOpacity
                                        style={styles.acceptBtn}
                                        onPress={() => handleAccept(item.id)}
                                        activeOpacity={0.7}
                                    >
                                        <Text style={styles.acceptBtnText}>Add</Text>
                                    </TouchableOpacity>
                                    <TouchableOpacity
                                        style={styles.rejectBtn}
                                        onPress={() => handleReject(item.id)}
                                        activeOpacity={0.7}
                                    >
                                        <X color="#999999" size={18} />
                                    </TouchableOpacity>
                                </View>
                            </View>
                        )}
                    />
                )}
            </View>
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: '#000000',
    },
    header: {
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'space-between',
        paddingTop: 56,
        paddingHorizontal: 16,
        paddingBottom: 16,
    },
    backBtn: {
        padding: 8,
    },
    logo: {
        color: '#FFFFFF',
        fontSize: 26,
        fontWeight: '800',
        letterSpacing: 0.5,
    },
    searchSection: {
        paddingHorizontal: 16,
        marginBottom: 28,
    },
    searchRow: {
        flexDirection: 'row',
        gap: 10,
    },
    searchInputContainer: {
        flex: 1,
    },
    input: {
        backgroundColor: '#1A1A1A',
        borderRadius: 10,
        paddingVertical: 12,
        paddingHorizontal: 16,
        fontSize: 15,
        color: '#FFFFFF',
    },
    searchBtn: {
        backgroundColor: '#FFFFFF',
        width: 46,
        borderRadius: 10,
        justifyContent: 'center',
        alignItems: 'center',
    },
    searchBtnDisabled: {
        opacity: 0.4,
    },
    error: {
        color: '#FF3B30',
        fontSize: 13,
        marginTop: 8,
    },
    requestsSection: {
        flex: 1,
        paddingHorizontal: 16,
    },
    sectionTitle: {
        color: '#999999',
        fontSize: 12,
        fontWeight: '700',
        letterSpacing: 1,
        marginBottom: 16,
    },
    emptyText: {
        color: '#666666',
        fontSize: 15,
        textAlign: 'center',
        marginTop: 40,
    },
    requestCard: {
        flexDirection: 'row',
        alignItems: 'center',
        paddingVertical: 12,
    },
    requestAvatar: {
        width: 44,
        height: 44,
        borderRadius: 22,
        backgroundColor: '#1A1A1A',
        justifyContent: 'center',
        alignItems: 'center',
        marginRight: 12,
    },
    requestAvatarText: {
        color: '#FFFFFF',
        fontSize: 18,
        fontWeight: '700',
    },
    requestInfo: {
        flex: 1,
    },
    requestName: {
        color: '#FFFFFF',
        fontSize: 16,
        fontWeight: '600',
    },
    requestHandle: {
        color: '#999999',
        fontSize: 13,
        marginTop: 1,
    },
    requestActions: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: 12,
    },
    acceptBtn: {
        backgroundColor: '#FFFFFF',
        paddingVertical: 7,
        paddingHorizontal: 20,
        borderRadius: 8,
    },
    acceptBtnText: {
        color: '#000000',
        fontSize: 13,
        fontWeight: '700',
    },
    rejectBtn: {
        padding: 4,
    },
});
